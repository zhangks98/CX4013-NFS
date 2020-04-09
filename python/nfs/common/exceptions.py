class Error(Exception):
    """Base class for exceptions in this module."""
    pass


class BadRequestError(Error):
    """Exception raised for illegal request."""
    pass


class NotFoundError(Error):
    """Exception raised for resources not found."""
    pass
