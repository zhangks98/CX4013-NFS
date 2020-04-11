import pytest
from pyfakefs.fake_filesystem import FakeFilesystem
from nfs.server.servicer import AMOServicer, ALOServicer
from nfs.common.requests import EmptyRequest, ReadRequest, WriteRequest
from nfs.common.values import Str, Int32, Bytes
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

    def test_handle_read(self, fs: FakeFilesystem):
        fs.create_file('test.txt', contents='test')
        req = ReadRequest(0)
        req.add_param(Int32(0))  # Offset
        req.add_param(Int32(0))  # Count
        req.add_param(Str("test.txt"))  # Path
        addr = "localhost"
        val = self.servicer.handle(req, addr)
        assert val[0].get_val() == 'test'

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

