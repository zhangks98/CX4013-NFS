from unittest import mock

import pytest
import os
from pyfakefs.fake_filesystem import FakeFilesystem

from nfs.common.requests import (EmptyRequest, GetAttrRequest, ReadRequest,
                                 TouchRequest, WriteRequest, RegisterRequest)
from nfs.common.exceptions import BadRequestError
from nfs.common.values import Bytes, Int32, Str
from nfs.server.servicer import ALOServicer, AMOServicer

addr = "localhost"
port = 8080
mock_socket = mock.Mock()
mock_socket.recv.return_value = []
mock_socket.getsockname.return_value = (addr, port)

ROOT_DIR = "."


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
        assert mtime == int(fs.stat('test.txt').st_mtime * 1000)
        assert atime == int(fs.stat('test.txt').st_atime * 1000)

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

    def test_handle_read_dir(self, fs: FakeFilesystem):
        fs.create_dir('test')
        req = ReadRequest(0)
        req.add_param(Str("test"))  # Path
        with pytest.raises(BadRequestError):
            self.servicer.handle(req, addr)

    def test_handle_register(self, fs: FakeFilesystem):
        file_path = 'test.txt'
        monitor_interval = 100
        fs.create_file(file_path, contents='test')
        req = RegisterRequest(0)
        req.add_param(Int32(monitor_interval))  # monitor_interval
        req.add_param(Str(file_path))  # Path
        # Register
        self.servicer.handle(req, addr)
        client_addr = addr
        # Should be able to find the normalized path in file_subscriber
        assert file_path in self.servicer.file_subscribers
        # Should be able to find the client identifier in file_subscriber
        assert client_addr in self.servicer.file_subscribers[file_path]
        # Should be able to find monitor_interval associated with that client
        assert self.servicer.file_subscribers[file_path][client_addr]["monitor_interval"] == monitor_interval
        # Update register
        monitor_interval = 200
        req = RegisterRequest(0)
        req.add_param(Int32(200))  # monitor_interval
        req.add_param(Str(file_path))  # Path
        self.servicer.handle(req, addr)
        assert self.servicer.file_subscribers[file_path][client_addr]["monitor_interval"] == monitor_interval

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
