Java Ro*u*teKit
---------------

Super-fast and lightweight Java URL router.

### Features

- **Fast**: highly efficient routing algorithm using hashtable index, prefix trie and finite state machine. No regexp matching.
- **GC friendly**: minimal number of allocations during routing. No `String` and `char[]` waste.
  **Small**: everything is one small library jar.
- **Scalable**: can handle thousands of rules without overhead.
- **Simple**: supports string variables and wildcards.
- **Lightweight**. No dependencies on other libraries. None. Zero.
- **Adaptable**: API does not require or force any web framework or library. Works with any `String` or `char[]` input.
- **Extensible**: supports options and extension points for new syntax.
