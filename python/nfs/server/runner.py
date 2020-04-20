import argparse
import logging
import random
import socket
from os.path import abspath, isdir

from nfs.common.exceptions import BadRequestError, NotFoundError
from nfs.common.requests import Request
from nfs.common.responses import Response, ResponseStatus
from nfs.common.serialize import BUF_SIZE
from nfs.common.values import Str
from nfs.server.servicer import ALOServicer, AMOServicer

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.INFO)

logger = logging.getLogger(__name__)


def send_response(sock: socket.socket, addr: str, res: Response, loss_prob: int):
    if random.random() < loss_prob:
        logger.info('Response to request #{} is lost'.format(res.get_req_id()))
        return
    sock.sendto(res.to_bytes(), addr)


def main():
    """The main function.\n
    Usage: `main.py [-h] -m {ALO,AMO} port path`\n
    Example: python main.py -m AMO 2222 .

    :raises OSError: [description]
    :raises RuntimeError: [description]
    """
    parser = argparse.ArgumentParser(
        description='Server for Remote File Access')
    parser.add_argument('port', type=int, help='Server port.')
    parser.add_argument(
        'path', type=str, help='Root directory for the server.')
    parser.add_argument(
        '-m', '--mode', choices=['ALO', 'AMO'], required=True, help='Invocation semantic.')
    parser.add_argument(
        '-l', '--loss-prob', default=0.0, type=float, help='Probability of a response loss; default is 0.0'
    )

    args = parser.parse_args()

    UDP_IP: str = '0.0.0.0'
    UDP_PORT: str = args.port
    ROOT_DIR: str = args.path
    MODE: str = args.mode
    LOSS_PROB: int = args.loss_prob

    if not isdir(ROOT_DIR):
        raise OSError('Path {} is not a directory'.format(ROOT_DIR))

    sock = socket.socket(socket.AF_INET,  # Internet
                         socket.SOCK_DGRAM)  # UDP
    sock.bind((UDP_IP, UDP_PORT))
    logger.info('Server running at port %s', UDP_PORT)

    if MODE == 'ALO':
        servicer = ALOServicer(ROOT_DIR, sock)
    elif MODE == 'AMO':
        servicer = AMOServicer(ROOT_DIR, sock)
    else:
        raise RuntimeError('Invalid invocation semantic.')

    logger.info('Server running in %s mode', MODE)
    logger.info('Root directory: %s', abspath(ROOT_DIR))
    logger.info('Loss probability: %s', LOSS_PROB)

    try:
        while True:
            data, addr = sock.recvfrom(BUF_SIZE)

            try:
                req = Request.from_bytes(data)
            except (ValueError, NotImplementedError) as e:
                logger.exception("Unable to parse request")
                res = Response(-1, ResponseStatus.BAD_REQUEST, [Str(str(e))])
                sock.sendto(res.to_bytes(), addr)
                continue

            logger.info('Received %s #%s from %s',
                        req.get_name().name, req.get_id(), addr)

            try:
                vals = servicer.handle(req, addr)
                res = Response(req.get_id(), ResponseStatus.OK, vals)
                send_response(sock, addr, res, LOSS_PROB)
            except BadRequestError as e:
                logger.warning('Bad request for %s: %s',
                               req.get_name().name, e)
                res = Response(
                    req.get_id(), ResponseStatus.BAD_REQUEST, [Str(str(e))])
                sock.sendto(res.to_bytes(), addr)
            except NotFoundError as e:
                logger.warning('Resources not found for %s: %s',
                               req.get_name().name, e)
                res = Response(
                    req.get_id(), ResponseStatus.NOT_FOUND, [Str(str(e))])
                sock.sendto(res.to_bytes(), addr)
            except Exception as e:
                logger.exception('Error handling %s', req.get_name().name)
                res = Response(
                    req.get_id(), ResponseStatus.INTERNAL_ERROR, [Str(str(e))])
                sock.sendto(res.to_bytes(), addr)

    except KeyboardInterrupt:
        logger.info('Shutting down...')
        sock.close()


if __name__ == "__main__":
    main()
