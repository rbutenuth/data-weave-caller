%dw 2.0
import * from dw::test::Tests
import * from dw::test::Asserts
import dwmodules::DemoModule

---

"DemoModule" describedBy [
	"result should be object" in do {
		DemoModule::main({ payload: "", vars: { b: "bar" } })
	    must equalTo({ message: "Hello world!", payload: "", variables: { b: "bar" } })
	}
]
