import pytest

from nfs.common.responses import Response, ResponseStatus
from nfs.common.values import Int32, Int64, Bytes, Str


def test_empty_response():
    values = []
    status = ResponseStatus.NOT_FOUND
    req_id = 0
    expected = Response(req_id, status, values)
    serialized = expected.to_bytes()
    actual = Response.from_bytes(serialized)
    assert actual.get_req_id() == req_id
    assert actual.get_status() == status
    assert actual.get_values() == []


def test_response_marshalling():
    values = [Int32(-(1 << 31)), Int64(-(1 << 63)),
              Str('hello'), Bytes(b'world')]
    req_id = 1
    status = ResponseStatus.OK
    expected = Response(req_id, status, values)
    serialized = expected.to_bytes()
    actual = Response.from_bytes(serialized)
    assert actual.get_req_id() == req_id
    assert actual.get_status() == status
    assert actual.get_py_values() == \
        [-(1 << 31), -(1 << 63), 'hello', b'world']


def test_invalid_response_marshalling():
    values = [1, 2, "hello", b'world']
    req_id = 1
    status = ResponseStatus.OK
    expected = Response(req_id, status, values)
    with pytest.raises(TypeError) as excinfo:
        expected.to_bytes()
    assert 'Illegal value type' in str(excinfo.value)
