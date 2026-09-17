#!/usr/bin/env python3
"""Translate the supported Eagler Java mod API subset into EBC bytecode."""
import argparse
import re

from ebc_assemble import assemble

LOG = re.compile(r'(?:EaglerMod|FMod)\.log\s*\(\s*"((?:\\.|[^"\\])*)"\s*\)\s*;')
COMMAND = re.compile(r'(?:EaglerMod|FMod)\.registerCommand\s*\(\s*"([a-z0-9._-]+)"\s*\)\s*;')
TICK = re.compile(r'(?:EaglerMod|FMod)\.onTick\s*\(\s*\)\s*;')
LOAD = re.compile(r'(?:EaglerMod|FMod)\.onLoad\s*\(\s*\)\s*;')
UNLOAD = re.compile(r'(?:EaglerMod|FMod)\.onUnload\s*\(\s*\)\s*;')
EVENT = re.compile(r'(?:EaglerMod|FMod)\.registerEvent\s*\(\s*"([a-z0-9._-]+)"\s*\)\s*;')
SET_CONFIG = re.compile(r'(?:EaglerMod|FMod)\.setConfig\s*\(\s*"([a-z0-9._-]+)"\s*,\s*"((?:\\.|[^"\\])*)"\s*\)\s*;')
REGISTER_KEYBIND = re.compile(r'(?:EaglerMod|FMod)\.registerKeybind\s*\(\s*"([a-z0-9._-]+)"\s*,\s*(\-?\d+)\s*\)\s*;')
ON_KEY_PRESSED = re.compile(r'(?:EaglerMod|FMod)\.onKeyPressed\s*\(\s*"([a-z0-9._-]+)"\s*,\s*(\-?\d+)\s*,\s*(true|false)\s*\)\s*;')
OPEN_SCREEN = re.compile(r'(?:EaglerMod|FMod)\.openScreen\s*\(\s*"([a-z0-9._-]+)"\s*\)\s*;')
REGISTER_CONFIG = re.compile(r'(?:EaglerMod|FMod)\.registerConfig\s*\(\s*"([a-z0-9._-]+)"\s*,\s*"((?:\\.|[^"\\])*)"\s*\)\s*;')
REGISTER_CONFIG = re.compile(r'(?:EaglerMod|FMod)\.registerConfig\s*\(\s*"([a-z0-9._-]+)"\s*,\s*"((?:\\.|[^"\\])*)"\s*\)\s*;')
CALL = re.compile(r'(?:EaglerMod|FMod)\.(?:log|registerCommand|onTick|onLoad|onUnload|registerEvent|setConfig|registerKeybind|onKeyPressed|openScreen|registerConfig)\s*\(')


def unescape(value):
    return bytes(value, "utf-8").decode("unicode_escape")


def translate(source):
    instructions = []
    spans = []
    for match in LOG.finditer(source):
        instructions.append("log " + unescape(match.group(1)))
        spans.append(match.span())
    for match in COMMAND.finditer(source):
        instructions.append("command " + match.group(1))
        spans.append(match.span())
    for match in TICK.finditer(source):
        instructions.append("tick")
        spans.append(match.span())
    for match in LOAD.finditer(source):
        instructions.append("load")
        spans.append(match.span())
    for match in UNLOAD.finditer(source):
        instructions.append("unload")
        spans.append(match.span())
    for match in EVENT.finditer(source):
        instructions.append("event " + match.group(1))
        spans.append(match.span())
    for match in SET_CONFIG.finditer(source):
        instructions.append("config " + match.group(1) + " " + unescape(match.group(2)))
        spans.append(match.span())
    for match in REGISTER_KEYBIND.finditer(source):
        instructions.append("keybind " + match.group(1) + " " + match.group(2))
        spans.append(match.span())
    for match in ON_KEY_PRESSED.finditer(source):
        instructions.append("key_pressed " + match.group(1) + " " + match.group(2) + " " + match.group(3))
        spans.append(match.span())
    for match in OPEN_SCREEN.finditer(source):
        instructions.append("screen " + match.group(1))
        spans.append(match.span())
    for match in REGISTER_CONFIG.finditer(source):
        instructions.append("register_config " + match.group(1) + " " + unescape(match.group(2)))
        spans.append(match.span())
    unsupported = [match for match in CALL.finditer(source)
                   if not any(start <= match.start() < end for start, end in spans)]
    if unsupported:
        raise ValueError("unsupported EaglerMod call near source offset " + str(unsupported[0].start()))
    if not instructions:
        raise ValueError("no supported EaglerMod calls found")
    return assemble("\n".join(instructions + ["end"]))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source")
    parser.add_argument("output")
    args = parser.parse_args()
    with open(args.source, encoding="utf-8") as source:
        data = translate(source.read())
    with open(args.output, "wb") as output:
        output.write(data)


if __name__ == "__main__":
    main()
