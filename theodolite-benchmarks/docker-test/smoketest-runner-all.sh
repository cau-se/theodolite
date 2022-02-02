#!/bin/sh

find . -name 'test.sh' -type f -exec dirname {} \; |
    sort |
    xargs -I %s sh -c "./smoketest-runner.sh %s 1>&2; echo $?" |
    sort |
    awk '{count[$1!=0]++} END {print count[0] " tests successful, " count[1] " test failed."}; exit count[1]'
