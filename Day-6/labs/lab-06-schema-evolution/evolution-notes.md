# Lab 06 — Schema evolution notes (deliverable)

Fill in after completing the lab.

| Change | Compatible? | Why |
|--------|-------------|-----|
| Add `email` with default `""` | Yes (BACKWARD) | Old readers ignore new field; new readers get default for old data |
| Rename `dept` → `department` (no alias) | No | Avro treats as delete + add → Registry returns **409** |
| Rename with `aliases: ["dept"]` | Yes | Registry maps old field name to new |

## My run

- Schema versions after Lab 04: `[1]`
- After `SchemaEvolutionDemo` / v2 register: `[1, 2]` (fill in)
- Evolved message seen: `id=99`, `Evolved-Employee`, `Sales`

## Registry URLs used

- Subjects: http://localhost:8081/subjects
- Versions: http://localhost:8081/subjects/employees-avro-value/versions
