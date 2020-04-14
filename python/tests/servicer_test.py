from unittest import mock

import pytest
from pyfakefs.fake_filesystem import FakeFilesystem

from nfs.common.requests import (EmptyRequest, GetAttrRequest, ReadRequest,
                                 TouchRequest, WriteRequest)
from nfs.common.exceptions import BadRequestError
from nfs.common.values import Bytes, Int32, Str
from nfs.server.servicer import ALOServicer, AMOServicer

mock_socket = mock.Mock()
mock_socket.recv.return_value = []
addr = "localhost"


def get_memory_addr(var):
    """
    Returns the actual memory address of that variable;
    """
    return hex(id(var))


class TestALOServier:
    def setup_method(self):
        self.servicer = ALOServicer(".", mock_socket)

    def test_handle_get_attr(self, fs: FakeFilesystem):
        fs.create_file('test.txt', contents='test')
        req = GetAttrRequest(0)
        req.add_param(Str('test.txt'))  # Path
        val = self.servicer.handle(req, addr)
        mtime = val[0].get_val()
        atime = val[1].get_val()
        assert mtime == int(fs.stat('test.txt').st_mtime)
        assert atime == int(fs.stat('test.txt').st_atime)

    def test_handle_touch(self, fs: FakeFilesystem):
        req = TouchRequest(0)
        assert fs.exists('test.txt') is False
        req.add_param(Str('test.txt'))  # Path
        self.servicer.handle(req, addr)
        assert fs.exists('test.txt') is True
        atime_old = fs.stat('test.txt').st_atime
        self.servicer.handle(req, addr)
        atime_new = fs.stat('test.txt').st_atime
        assert atime_new != atime_old

    def test_handle_write(self, fs: FakeFilesystem):
        fs.create_file('test.txt', contents='test')
        req = WriteRequest(0)
        req.add_param(Int32(2))  # Offset
        req.add_param(Str('test.txt'))  # Path
        req.add_param(Bytes(b'INSERT'))
        self.servicer.handle(req, addr)
        with open('test.txt', 'rb') as f:
            content = f.read()
            assert content == b'teINSERTst'
        fs.create_dir('text.txt')
        self.servicer.handle(req, addr)

    def test_handle_write_dir(self, fs: FakeFilesystem):
        fs.create_dir('dir')
        req = WriteRequest(0)
        req.add_param(Int32(2))  # Offset
        req.add_param(Str('dir'))  # Path
        req.add_param(Bytes(b'INSERT'))
        with pytest.raises(BadRequestError):
            self.servicer.handle(req, addr)

    def test_handle_read(self, fs: FakeFilesystem):
        fs.create_file('test.txt', contents='test')
        req = ReadRequest(0)
        req.add_param(Str("test.txt"))  # Path
        val = self.servicer.handle(req, addr)
        assert val[0].get_val() == b'test'

    def test_duplicate_request(self):
        req = EmptyRequest(0)
        val_a = self.servicer.handle(req, addr)
        val_b = self.servicer.handle(req, addr)
        assert get_memory_addr(val_a) != get_memory_addr(val_b)


class TestAMOServicer:
    def setup_method(self):
        self.servicer = AMOServicer(".", mock_socket)

    def test_duplicate_request(self):
        req = EmptyRequest(0)
        assert self.servicer._is_duplicate_request(req, addr) is None
        val_a = self.servicer.handle(req, addr)
        assert self.servicer._is_duplicate_request(req, addr) is not None
        val_b = self.servicer.handle(req, addr)
        assert get_memory_addr(val_a) == get_memory_addr(val_b)
