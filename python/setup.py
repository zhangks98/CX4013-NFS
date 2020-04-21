from setuptools import find_packages, setup

setup(
    name="nfs-server",
    description='The server for remote file access',
    url='https://github.com/zhangks98/CX4013-NFS',
    version='1.0.0',
    packages=find_packages(),
    python_requires='>=3.6',
    install_requires=[],
    extras_require={
        'dev': ['isort', 'autopep8'],
        'test': ['flake8', 'pytest', 'pyfakefs'],
    },
    entry_points={
        'console_scripts': [
            'nfs-server=nfs.server.runner:main',
        ],
    },
)
