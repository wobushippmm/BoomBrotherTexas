#!/bin/sh
cd `dirname $0`

protoc --js_out=import_style=commonjs,binary:. protocol.proto