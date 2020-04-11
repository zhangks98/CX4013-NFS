from unittest import mock

import pytest
from pyfakefs.fake_filesystem import FakeFilesystem

from nfs.common.requests import (EmptyRequest, GetAttrRequest, ReadRequest,
                                 TouchRequest, WriteRequest)
from nfs.common.values import Bytes, Int32, Str
from nfs.server.servicer import ALOServicer, AMOServicer

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

    def test_handle_get_Attr(self, fs: FakeFilesystem):
        fs.create_file('test.txt', contents='test')
        req = GetAttrRequest(0)
        req.add_param(Str('test.txt'))  # Path
        addr = "localhost"
        val = self.servicer.handle(req, addr)
        mtime = val[0].get_val()
        atime = val[1].get_val()
        assert mtime == int(fs.stat('test.txt').st_mtime)
        assert atime == int(fs.stat('test.txt').st_atime)

    def test_handle_touch(self, fs: FakeFilesystem):
        req = TouchRequest(0)
        assert fs.exists('test.txt') is False
        req.add_param(Str('test.txt'))  # Path
        addr = "localhost"
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
        addr = "localhost"
        self.servicer.handle(req, addr)
        with open('test.txt', 'rb') as f:
            content = f.read()
            assert content == b'teINSERTst'

    def test_handle_read(self, fs: FakeFilesystem):
        fs.create_file('test.txt', contents='test')
        req = ReadRequest(0)
        req.add_param(Str("test.txt"))  # Path
        addr = "localhost"
        val = self.servicer.handle(req, addr)
        assert val[0].get_val() == b'test'

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
