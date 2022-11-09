%dw 2.0
import * from dw::test::Tests
import * from dw::test::Asserts

import * from MyModule
---
"MyModule" describedBy [
    "helloWorld" describedBy [
        "result should be object" in do {
            helloWorld() must equalTo({ hello: "world" })
        },
        "1+2 should be 3" in do {
            (1 + 2) must equalTo(3)
        },
        "6*7 should be 42" in do {
            (6 * 7) must equalTo(41)
        }
    ]
]
