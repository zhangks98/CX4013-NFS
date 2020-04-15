from struct import pack_into

import pytest

from nfs.common.requests import (EmptyRequest, FileUpdatedCallback,
                                 GetAttrRequest, ListDirRequest, ReadRequest,
                                 RegisterRequest, Request, RequestName,
                                 TouchRequest, WriteRequest)
from nfs.common.serialize import ByteBuffer
from nfs.common.values import Bytes, Int32, Str, Value


def test_should_not_marshall_mismatched_param():
    expected = GetAttrRequest(1)
    with pytest.raises(ValueError) as excinfo:
        expected.to_bytes()
    assert 'wrong number of parameters' in str(excinfo.value)


def test_should_not_marshall_invalid_param_type():
    expected = ListDirRequest(2)
    expected.add_param("readme.txt")
    with pytest.raises(TypeError) as excinfo:
        expected.to_bytes()
    assert 'Illegal value type' in str(excinfo.value)


def test_should_not_unmarshal_mismatched_param():
    expected = EmptyRequest(3)
    serialized = expected.to_bytes()
    buf = bytearray(serialized)
    # Sets numParams to 2 in serialized message.
    pack_into('>i', buf, 5, 2)
    with pytest.raises(ValueError) as excinfo:
        Request.from_bytes(buf)
    assert 'wrong number of parameters' in str(excinfo.value)


def test_should_not_unmarshal_FileUpdatedCallback():
    path = 'secrets.txt'
    mtime = 123
    data = b'password'
    expected = FileUpdatedCallback(path, mtime, data)
    serialized = expected.to_bytes()
    with pytest.raises(NotImplementedError) as excinfo:
        Request.from_bytes(serialized)
    assert 'FileUpdatedCallback' in str(excinfo.value)


def test_marshall_FileUpdatedCallback():
    path = 'secrets.txt'
    mtime = 123
    data = b'password'
    expected = FileUpdatedCallback(path, mtime, data)
    buf = ByteBuffer.wrap(expected.to_bytes())
    assert buf.get_int() == 0
    assert buf.get() == RequestName.FILE_UPDATED.value
    assert buf.get_int() == RequestName.FILE_UPDATED.num_params
    assert Value.from_bytes(buf).get_val() == path
    assert Value.from_bytes(buf).get_val() == mtime
    assert Value.from_bytes(buf).get_val() == data


def test_unmarshal_EmptyRequest():
    req_id = -(1 >> 31)
    expected = EmptyRequest(req_id)
    actual = Request.from_bytes(expected.to_bytes())
    assert actual.get_id() == req_id
    assert actual.get_name() == RequestName.EMPTY


def test_unmarshal_ReadRequest():
    req_id = 1
    path = 'abc.txt'
    expected = ReadRequest(req_id)
    expected.add_param(Str(path))
    actual = Request.from_bytes(expected.to_bytes())
    assert actual.get_id() == req_id
    assert actual.get_name() == RequestName.READ
    assert actual.get_path() == path


def test_unmarshal_WriteRequest():
    req_id = 4
    offset = 5
    path = 'abc.txt'
    data = bytes([0xd, 0xe, 0xf])
    expected = WriteRequest(req_id)
    expected.add_param(Int32(offset))
    expected.add_param(Str(path))
    expected.add_param(Bytes(data))
    actual = Request.from_bytes(expected.to_bytes())
    assert actual.get_id() == req_id
    assert actual.get_name() == RequestName.WRITE
    assert actual.get_path() == path
    assert actual.get_offset() == offset
    assert actual.get_data() == data


def test_unmarshal_GetAttrRequest():
    req_id = 1
    path = 'hello.txt'
    expected = GetAttrRequest(req_id)
    expected.add_param(Str(path))
    actual = Request.from_bytes(expected.to_bytes())
    assert actual.get_id() == req_id
    assert actual.get_name() == RequestName.GET_ATTR
    assert actual.get_path() == path


def test_unmarshal_TouchRequest():
    req_id = 1
    path = 'world.txt'
    expected = TouchRequest(req_id)
    expected.add_param(Str(path))
    actual = Request.from_bytes(expected.to_bytes())
    assert actual.get_id() == req_id
    assert actual.get_name() == RequestName.TOUCH
    assert actual.get_path() == path


def test_unmarshal_ListDirRequest():
    req_id = 1
    path = 'src/test'
    expected = ListDirRequest(req_id)
    expected.add_param(Str(path))
    actual = Request.from_bytes(expected.to_bytes())
    assert actual.get_id() == req_id
    assert actual.get_name() == RequestName.LIST_DIR
    assert actual.get_path() == path


def test_unmarshal_RegisterRequest():
    req_id = 1
    path = 'abc.txt'
    monitor_interval = 100
    expected = RegisterRequest(req_id)
    expected.add_param(Int32(monitor_interval))
    expected.add_param(Str(path))
    actual = Request.from_bytes(expected.to_bytes())
    assert actual.get_id() == req_id
    assert actual.get_name() == RequestName.REGISTER
    assert actual.get_path() == path
    assert actual.get_monitor_interval() == monitor_interval
