import argparse
import logging
import socket
from os.path import isdir

from nfs.common.exceptions import BadRequestError, NotFoundError
from nfs.common.requests import Request
from nfs.common.responses import Response, ResponseStatus
from nfs.common.serialize import BUF_SIZE
from nfs.server.servicer import ALOServicer, AMOServicer

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.INFO)

logger = logging.getLogger(__name__)


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
    args = parser.parse_args()

    UDP_IP = 'localhost'
    UDP_PORT = args.port
    ROOT_DIR = args.path
    MODE = args.mode

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
    logger.info('Root directory: %s', ROOT_DIR)

    try:
        while True:
            data, addr = sock.recvfrom(BUF_SIZE)

            try:
                req = Request.from_bytes(data)
            except (ValueError, NotImplementedError) as exp:
                logger.exception("Unable to parse request")
                res = Response(req.get_id(), ResponseStatus.BAD_REQUEST)
                sock.sendto(res.to_bytes(), addr)
                continue

            logger.info('Received %s', req.get_name().name)

            try:
                vals = servicer.handle(req, addr)
                res = Response(req.get_id(), ResponseStatus.OK, vals)
                sock.sendto(res.to_bytes(), addr)
            except BadRequestError as e:
                logger.warning('Bad request for %s: %s',
                               req.get_name().name, e)
                res = Response(req.get_id(), ResponseStatus.BAD_REQUEST)
                sock.sendto(res.to_bytes(), addr)
            except NotFoundError as e:
                logger.warning('Resources not found for %s: %s',
                               req.get_name().name, e)
                res = Response(req.get_id(), ResponseStatus.NOT_FOUND)
                sock.sendto(res.to_bytes(), addr)
            except Exception as e:
                logger.exception('Error handling %s', req.get_name().name)
                res = Response(req.get_id(), ResponseStatus.INTERNAL_ERROR)
                sock.sendto(res.to_bytes(), addr)

    except KeyboardInterrupt:
        logger.info('Shuting down...')
        sock.close()


if __name__ == "__main__":
    main()