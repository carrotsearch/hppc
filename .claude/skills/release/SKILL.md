---
name: release
description: Release a new HPPC version - Maven Central, git tag, javadocs on gh-pages, GitHub release page, next snapshot bump.
---

# HPPC release

`X.Y.Z` is the version being released. Steps 3-6 are public and irreversible: confirm
with the user before each one. Stop on any failure; never re-publish or move a pushed tag.

## 1. Prepare (on `master`, clean, in sync with origin)

- `CHANGES.txt`: the top heading must be `[X.Y.Z]`. Compare its entries against
  `git log --no-merges <previous-tag>..HEAD` and merged PRs; every change that affects users
  needs an entry (`GH-nnn: description. (Author)`), no `GH-xxx` placeholders.
- The GitHub milestone `X.Y.Z` must exist and contain the resolved issues/PRs:
  `gh api 'repos/carrotsearch/hppc/milestones?state=all'`.
- `build-options.properties`: set `project.version=X.Y.Z` (drop `-SNAPSHOT`).
- `./gradlew clean check` must pass (this also builds the javadocs).
- Commit: `Bump version to X.Y.Z`.

## 2. Inspect artifacts (optional)

`./gradlew publishLocal` (-> `build/maven`) or `./gradlew prepareMavenCentralBundle`
(-> `build/maven-bundle`).

## 3. Publish to Maven Central

```
./gradlew publishToMavenCentral
```

Signing keys and Central Portal credentials come from the user's own gradle properties; if
they are missing, ask - do not look for them. Verify the version eventually shows up in
https://repo1.maven.org/maven2/com/carrotsearch/hppc/maven-metadata.xml (can take a while).

## 4. Tag

Lightweight tag named exactly as the version, on the version bump commit:

```
git tag X.Y.Z && git push origin master X.Y.Z
```

## 5. Javadocs (gh-pages)

Javadocs live on the `gh-pages` branch under `releases/X.Y.Z/api/`. Use a temporary
worktree, do not switch branches in the main checkout:

```
git fetch origin gh-pages
git worktree add <scratch>/gh-pages origin/gh-pages -B gh-pages
mkdir -p <scratch>/gh-pages/releases/X.Y.Z
cp -r hppc/build/docs/javadoc <scratch>/gh-pages/releases/X.Y.Z/api
(cd <scratch>/gh-pages && git add releases/X.Y.Z && git commit -m "Javadoc for X.Y.Z" && git push origin gh-pages)
git worktree remove <scratch>/gh-pages
```

The javadoc must be the one built from the tagged commit. Verify
https://carrotsearch.github.io/hppc/releases/X.Y.Z/api/ returns 200 (takes a minute or two).

## 6. GitHub release page

Follow the format of the previous release (`gh release view <previous-tag>`):

```
<one short paragraph summarizing the release; mention incompatibilities and minimum Java>

Resolved issues:
https://github.com/carrotsearch/hppc/milestone/<N>?closed=1

JavaDoc:
https://carrotsearch.github.io/hppc/releases/X.Y.Z/api/

<the [X.Y.Z] section of CHANGES.txt: "** Section" -> "## Section", entries -> "* GH-nnn: ...">
```

Write it to a scratch file, show it to the user, then:

```
gh release create X.Y.Z --verify-tag --title "Release X.Y.Z" --notes-file <file>
```

No assets are attached. Releases notify watchers: use `--draft` if the user wants to review
on GitHub first.

## 7. Wrap up

- Close milestone `X.Y.Z`; create the next one:
  `gh api -X PATCH repos/carrotsearch/hppc/milestones/<N> -f state=closed`.
- `build-options.properties`: `project.version=<next>-SNAPSHOT`; add an empty `[<next>]`
  heading at the top of `CHANGES.txt`. Commit `Bump version to <next>-SNAPSHOT`, push.
