#!/usr/bin/env python3
"""Assemble a simple Eagler bytecode text file into an .ebc binary payload.

Supported lines:
  log message text
  command command_name
  tick
  load
  unload
  event event_name
  config key value
  register_config key default_value
  keybind name keycode
  key_pressed name keycode pressed
  screen screen_name
  end
"""
import argparse
import struct

MAX_STRING_BYTES = 4096


def encode_string(value):
    data = value.encode("utf-8")
    if len(data) > MAX_STRING_BYTES:
        raise ValueError("EBC string is too long")
    return struct.pack(">H", len(data)) + data


def assemble(source):
    output = bytearray(b"EBC1\x01")
    ended = False
    for line_number, line in enumerate(source.splitlines(), 1):
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        command, _, argument = line.partition(" ")
        if command == "log":
            output.append(1)
            output.extend(encode_string(argument))
        elif command == "command" and argument:
            output.append(2)
            output.extend(encode_string(argument))
        elif command == "tick":
            output.append(3)
        elif command == "load" and not argument:
            output.append(4)
        elif command == "unload" and not argument:
            output.append(5)
        elif command == "event" and argument:
            output.append(6)
            output.extend(encode_string(argument))
        elif command == "config" and argument:
            key, _, value = argument.partition(" ")
            if not key or not _ or not value:
                raise ValueError(f"invalid config instruction on line {line_number}")
            output.append(7)
            output.extend(encode_string(key))
            output.extend(encode_string(value))
        elif command in ("register_config", "config_register") and argument:
            key, _, value = argument.partition(" ")
            if not key or not _ or not value:
                raise ValueError(f"invalid register_config instruction on line {line_number}")
            output.append(11)
            output.extend(encode_string(key))
            output.extend(encode_string(value))
        elif command == "keybind" and argument:
            key_name, _, key_code = argument.partition(" ")
            if not key_name or not _ or not key_code:
                raise ValueError(f"invalid keybind instruction on line {line_number}")
            output.append(8)
            output.extend(encode_string(key_name))
            output.extend(struct.pack(">i", int(key_code)))
        elif command in ("key_pressed", "key-pressed", "keypress") and argument:
            key_name, _, rest = argument.partition(" ")
            if not key_name or not rest:
                raise ValueError(f"invalid key pressed instruction on line {line_number}")
            key_code, _, pressed = rest.partition(" ")
            if not key_code or not _ or not pressed:
                raise ValueError(f"invalid key pressed instruction on line {line_number}")
            output.append(9)
            output.extend(encode_string(key_name))
            output.extend(struct.pack(">i", int(key_code)))
            output.append(1 if pressed.lower() in ("1", "true", "yes", "on") else 0)
        elif command == "screen" and argument:
            output.append(10)
            output.extend(encode_string(argument))
        elif command == "end" and not argument:
            output.append(0)
            ended = True
        else:
            raise ValueError(f"invalid EBC instruction on line {line_number}")
    if not ended:
        raise ValueError("EBC source must end with: end")
    return bytes(output)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source")
    parser.add_argument("output")
    args = parser.parse_args()
    with open(args.source, encoding="utf-8") as source:
        data = assemble(source.read())
    with open(args.output, "wb") as output:
        output.write(data)


if __name__ == "__main__":
    main()
