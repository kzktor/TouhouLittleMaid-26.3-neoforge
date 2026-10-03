# aquaculture stub

`Aquaculture` has no MC 26.3 build (latest upstream is `2.9.2`, for 26.1.x), and TLM lists it as a
plain `implementation` dependency. Rather than delete TLM's compat layer, this source set provides
just enough of Aquaculture's API surface for `compat/aquaculture/**` to compile.

## How it is wired

`build.gradle` adds this directory as its own source set and puts **only its compiled output** on
`main`'s *compile* classpath:

```groovy
sourceSets { aquacultureStub { java.srcDir 'src/aquacultureStub/java' } }
dependencies { compileOnly sourceSets.aquacultureStub.output }
```

Consequences, all deliberate:

- The classes are **compile-only**. They are not on the runtime classpath and not in the jar, so a
  real Aquaculture jar can never collide with them.
- Because the reference is `sourceSets.<name>.output` rather than a directory, Gradle wires
  `compileAquacultureStubJava` ahead of `compileJava` automatically.
- At runtime the compat layer is inert: `AquacultureCompat.init` gates everything behind
  `FMLLoader...getModFileById("aquaculture") != null`, and `registerFishingType` is called from
  `FishingTypeManager` which is likewise only reached when Aquaculture is installed.

## Keeping it honest

The stubs mirror the signatures TLM actually calls and nothing more — no speculative members. The
one place that matters is `Hooks.EMPTY`: TLM compares hooks against it by identity, so the stub
supplies a distinct sentinel instance.

`AquaSounds`' suppliers throw instead of returning a placeholder, so that if this code ever *did*
run without the real mod, it would fail loudly rather than silently play the wrong sound.

## Removing it

When Aquaculture ships a 26.3 build, delete this directory, drop the `aquacultureStub` source set and
the `compileOnly` line from `build.gradle`, and restore the Modrinth dependency:

```groovy
implementation "maven.modrinth:aquaculture:<26.3 version>-neoforge"
```
