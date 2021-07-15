# socket_server
This repo is a proof of concept socket server written in java (a language i know) as the assuption is that the production server will be written in python.

## features

- server passcode
- client nickname

## connection

In order to connect successfully to the server, the server expects several things from the client. Each item is expected to be sent individually separated by a line break (if linux) or a line break and carriage return (if windows) :-

- message 1: server passcode e.g "A4lz0"
- message 2: client nickname, prefixed with '@' e.g "@Simon"
- message 3-?: UNDEFINED
- message ?: disconnect by sending '.' e.g. "."

The server will reply to each message so that the handshake process can continue - this reply can be an error message by will usually be the input originally sent :-

- message 1: reply with passcode if valid or "INCORRECT_PASSCODE" if invalid.
- message 2: reply with nickname if valid or "NICKNAME_IN_USE" if invalid.
- message ?: reply with "GOODBYE" only.
