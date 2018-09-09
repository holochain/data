## Install

Install [boot](https://github.com/boot-clj/boot).

Mac OS X:

`brew install boot-clj`

## Environment

`.lein-env` EDN file as per [environ](https://github.com/weavejester/environ).

See `.example-lein-env` for basic setup.

- `:github-token` a personal access Github token with read access to repos
- `:travis-token` access token for Travis generated with CLI client

## Run

2 terminal tabs.

`$ boot repl-server`. wait for port.

`$ boot repl-client`. start running repl commands.

## Reports

### cycle time plots

Histogram and time series of PR cycle time.

- cycle time is end minus start times for PRs reported by Github
- cycle time is measured in hours
- time series is plotted against start time
- username and repository must be provided
- default is only closed PRs against the `develop` branch are counted
- parameters can be overridden as a third param

```clojure
(report.pr-cycle-time/do-it! user repo)
(report.pr-cycle-time/do-it! "holochain" "holochain-rust")
(report.pr-cycle-time/do-it! user repo params)
(report.pr-cycle-time/do-it! "holochain" "holochain-rust" {:base "develop" :state "all"})
```

### build time plots

Histogram and time series of Travis build times.

- build time is end minus start times for builds reported by Travis
- build time is measured in minutes
- duration provided by Travis is NOT used as this is wall time not build time
- username and repository must be provided
- only passing builds are counted

```clojure
(report.build-duration/do-it! user repo)
(report.build-duration/do-it! "holochain" "holochain-rust")
```
