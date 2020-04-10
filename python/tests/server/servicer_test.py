from nfs.server.servicer import AMOServicer, ALOServicer
from nfs.common.requests import EmptyRequest
from unittest import mock

mock_socket = mock.Mock()
mock_socket.recv.return_value = []


def get_memory_addr(var):
    """
    Returns the actual memory address of that variable;
    """
    return hex(id(var))


class TestALOServier:
    def setup_method(self):
        self.servicer = ALOServicer(".", mock_socket)

    def test_duplicate_request(self):
        req = EmptyRequest(0)
        addr = "localhost"
        val_a = self.servicer.handle(req, addr)
        val_b = self.servicer.handle(req, addr)
        assert get_memory_addr(val_a) != get_memory_addr(val_b)

class TestAMOServicer:
    def setup_method(self):
        self.servicer = AMOServicer(".", mock_socket)

    def test_duplicate_request(self):
        req = EmptyRequest(0)
        addr = "localhost"
        assert self.servicer._is_duplicate_request(req, addr) is None
        val_a = self.servicer.handle(req, addr)
        assert self.servicer._is_duplicate_request(req, addr) is not None
        val_b = self.servicer.handle(req, addr)
        assert get_memory_addr(val_a) == get_memory_addr(val_b)

