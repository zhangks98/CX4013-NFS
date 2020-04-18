class Client:
    def __init__(self, addr: str, port: int):
        self.addr = addr
        self.port = port

    def get_addr(self):
        return self.addr

    def get_port(self):
        return self.port

    def get_hash(self):
        return str(self.addr) + ":" + str(self.port)
