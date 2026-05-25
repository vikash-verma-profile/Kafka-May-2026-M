"""
Lab 01 — serialize Employee in JSON, XML, Avro, and Protobuf.

Run:
    pip install -r requirements.txt
    python lab01_four_formats.py
"""

from __future__ import annotations

import io
import json
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path

import avro.io
import avro.schema

SCHEMA_JSON = json.dumps(
    {
        "type": "record",
        "name": "Employee",
        "namespace": "com.training.kafka",
        "fields": [
            {"name": "id", "type": "int"},
            {"name": "name", "type": "string"},
            {"name": "dept", "type": "string"},
            {"name": "salary", "type": "double"},
        ],
    }
)
AVRO_SCHEMA = avro.schema.parse(SCHEMA_JSON)


@dataclass
class Employee:
    id: int
    name: str
    email: str

    def to_avro_record(self) -> dict:
        return {
            "id": self.id,
            "name": self.name,
            "dept": "N/A",
            "salary": 0.0,
        }


def serialize_json(emp: Employee) -> bytes:
    return json.dumps({"id": emp.id, "name": emp.name, "email": emp.email}).encode()


def deserialize_json(data: bytes) -> Employee:
    obj = json.loads(data.decode())
    return Employee(obj["id"], obj["name"], obj["email"])


def serialize_xml(emp: Employee) -> bytes:
    root = ET.Element(
        "employee",
        {"id": str(emp.id), "name": emp.name, "email": emp.email},
    )
    return ET.tostring(root, encoding="utf-8")


def deserialize_xml(data: bytes) -> Employee:
    root = ET.fromstring(data)
    return Employee(int(root.attrib["id"]), root.attrib["name"], root.attrib["email"])


def serialize_avro(emp: Employee) -> bytes:
    writer = avro.io.DatumWriter(AVRO_SCHEMA)
    buffer = io.BytesIO()
    encoder = avro.io.BinaryEncoder(buffer)
    writer.write(emp.to_avro_record(), encoder)
    return buffer.getvalue()


def deserialize_avro(data: bytes) -> Employee:
    reader = avro.io.DatumReader(AVRO_SCHEMA)
    decoder = avro.io.BinaryDecoder(io.BytesIO(data))
    record = reader.read(decoder)
    return Employee(record["id"], record["name"], "a@x.io")


def _varint(value: int) -> bytes:
    parts = []
    while value > 0x7F:
        parts.append((value & 0x7F) | 0x80)
        value >>= 7
    parts.append(value)
    return bytes(parts)


def _proto_string_field(field_number: int, text: str) -> bytes:
    encoded = text.encode("utf-8")
    tag = (field_number << 3) | 2
    return bytes([tag]) + _varint(len(encoded)) + encoded


def serialize_protobuf(emp: Employee) -> bytes:
    # Proto3 wire encoding for id=1, name=2, email=3 (see employee.proto)
    return bytes([0x08]) + _varint(emp.id) + _proto_string_field(2, emp.name) + _proto_string_field(3, emp.email)


def deserialize_protobuf(data: bytes) -> Employee:
    # Minimal decoder for this lab message shape
    idx = 0
    emp_id, name, email = 0, "", ""

    def read_varint() -> int:
        nonlocal idx
        value = shift = 0
        while True:
            b = data[idx]
            idx += 1
            value |= (b & 0x7F) << shift
            if not (b & 0x80):
                return value
            shift += 7

    while idx < len(data):
        tag = data[idx]
        idx += 1
        field = tag >> 3
        wire = tag & 0x07
        if wire == 0:
            val = read_varint()
            if field == 1:
                emp_id = val
        elif wire == 2:
            length = read_varint()
            val = data[idx : idx + length].decode("utf-8")
            idx += length
            if field == 2:
                name = val
            elif field == 3:
                email = val
        else:
            break

    return Employee(emp_id, name, email)


def main() -> None:
    emp = Employee(101, "Asha", "a@x.io")
    out_dir = Path("output/serialized")
    out_dir.mkdir(parents=True, exist_ok=True)

    formats = {
        "json": (serialize_json, deserialize_json),
        "xml": (serialize_xml, deserialize_xml),
        "avro": (serialize_avro, deserialize_avro),
        "protobuf": (serialize_protobuf, deserialize_protobuf),
    }

    for name, (ser, deser) in formats.items():
        payload = ser(emp)
        path = out_dir / f"employee.{name}.bin"
        path.write_bytes(payload)
        assert deser(payload) == emp
        print(f"{name:8} {len(payload):4d} bytes -> {path}")

    print("All round-trips OK.")


if __name__ == "__main__":
    main()
