# markdown-blocks

[![CI](https://github.com/Meko123456/markdown-blocks/actions/workflows/ci.yml/badge.svg)](https://github.com/Meko123456/markdown-blocks/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A tiny, **dependency-free Kotlin Multiplatform markdown parser** that turns text into a
render-agnostic tree of blocks and spans — so you can render it **natively** on every
platform instead of shipping a WebView.

```kotlin
val blocks = Markdown.parse("# Hello\nSome **bold** text.")
// [Heading(level=1, spans=[Text("Hello")]),
//  Paragraph(spans=[Text("Some "), Bold("bold"), Text(" text.")])]
```

## Why

Most markdown libraries either render straight to HTML or bind you to one UI toolkit.
This one does neither: it hands you plain data. The same parse can drive a Jetpack Compose
`AnnotatedString` on Android, a SwiftUI `Text` on iOS, or anything else you write.

- 🪶 **No dependencies** — pure Kotlin, nothing but the stdlib
- 🎯 **Render-agnostic** — a data tree, zero UI opinions
- 🌍 **Multiplatform** — Android, iOS, and JVM targets
- ✅ **Tested** — 34 tests covering blocks, inline spans, and malformed input

## Install

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.meko123456:markdown-blocks:0.1.0")
}
```

> **Not on Maven Central yet.** That is the coordinate this library will ship under, but the release
> pipeline has never completed a run — `0.1.0` is still blocked on the Maven Central signing key — so
> it does not resolve today. Until it does, build from source: clone the repo and either add
> `includeBuild("../markdown-blocks")` to your `settings.gradle.kts`, or run
> `./gradlew publishToMavenLocal` and add `mavenLocal()` to your repositories.

## What it parses

**Blocks:** headings (`#`–`######`), paragraphs, bullet lists (`-` `*` `+`), ordered lists
(`1.` `1)`), block quotes (`>`), fenced code (```` ``` ```` with an optional language), and
horizontal rules (`---` `***` `___`).

**Inline:** `**bold**`, `__bold__`, `*italic*`, `_italic_`, `` `code` ``, `~~strikethrough~~`,
and `[links](https://example.com)`.

Malformed input never throws — unmatched markers are kept as literal text, and emphasis
follows CommonMark's flanking rule so `2 * 3 * 4` stays arithmetic rather than becoming italics.

## Rendering it

The library gives you data; rendering is a short `when`. On Android:

```kotlin
@Composable
fun MarkdownText(blocks: List<MdBlock>) {
    Column {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> Text(inline(block.spans), style = headingStyle(block.level))
                is MdBlock.Paragraph -> Text(inline(block.spans))
                is MdBlock.BulletItem -> Row { Text("• "); Text(inline(block.spans)) }
                // …
                MdBlock.Divider -> HorizontalDivider()
            }
        }
    }
}

private fun inline(spans: List<MdSpan>) = buildAnnotatedString {
    spans.forEach { span ->
        when (span) {
            is MdSpan.Text -> append(span.text)
            is MdSpan.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(span.text) }
            is MdSpan.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(span.text) }
            // …
        }
    }
}
```

The same tree renders on iOS by folding the spans into a SwiftUI `Text`. See
**[Nishani](https://github.com/Meko123456/Nishani)** for a full app doing exactly that on
both platforms.

## Scope

This is intentionally a *small* subset — the markdown people actually write in notes and
READMEs. It does not do tables, footnotes, HTML passthrough, or nested block structures.
If you need full CommonMark, use a full CommonMark parser.

## License

[MIT](LICENSE)
