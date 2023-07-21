# Theodolite Docs

Theodolite's docs are generated with Jekyll from Markdown files.

## Installation

To set up Jekyll run:

```sh
gem install bundler
bundle config set --local path 'vendor'
bundle install
```

## Local Testing

For live serving the docs run:

```sh
bundle exec jekyll serve
```

## Building

You can compile everything to HTML via:

```sh
bundle exec jekyll build
```

## CRD API Reference

We use the [crdoc](https://github.com/fybrik/crdoc) tool to generate the API reference for our CRDs:

```sh
crdoc --resources ../theodolite/crd/ --template api-reference/crds.tmpl  --output api-reference/crds.md
```

With the following command, crdoc is executed in Docker:

```sh
docker run --rm -v "`pwd`/../theodolite/crd/":/crd -v "`pwd`/api-reference":/api-reference ghcr.io/fybrik/crdoc:0.6.2 --resources /crd/ --template /api-reference/crds.tmpl --output /api-reference/crds.md
```
