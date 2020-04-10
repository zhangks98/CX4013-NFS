import unittest
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


class TestALOServier(unittest.TestCase):
    def setUp(self):
        self.servicer = ALOServicer(".", mock_socket)

    def test_duplicate_request(self):
        req = EmptyRequest(0)
        addr = "localhost"
        val_a = self.servicer.handle(req, addr)
        val_b = self.servicer.handle(req, addr)
        self.assertNotEquals(get_memory_addr(val_a), get_memory_addr(val_b))


class TestAMOServicer(unittest.TestCase):
    def setUp(self):
        self.servicer = AMOServicer(".", mock_socket)

    def test_duplicate_request(self):
        req = EmptyRequest(0)
        addr = "localhost"
        self.assertIsNone(self.servicer._is_duplicate_request(req, addr))
        val_a = self.servicer.handle(req, addr)
        self.assertIsNotNone(self.servicer._is_duplicate_request(req, addr))
        val_b = self.servicer.handle(req, addr)
        self.assertEqual(get_memory_addr(val_a), get_memory_addr(val_b))


if __name__ == '__main__':
    unittest.main()
