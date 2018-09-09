## Install

Install [boot](https://github.com/boot-clj/boot).

Mac OS X:

`brew install boot-clj`

## Environment

`.lein-env` EDN file as per [environ](https://github.com/weavejester/environ).

See `.example-lein-env` for basic setup.

- `:github-token` a personal access Github token with read access to repos

## Run

2 terminal tabs.

`$ boot repl-server`. wait for port.

`$ boot repl-client`. start running repl commands.

## Reports

### cycle time plots

Histogram and time series of PR cycle time.

- cycle time is end minus start times for PRs reported by github
- cycle time is measured in hours
- time series is plotted against start time
- username and repository must be provided
- default is only closed PRs against the `develop` branch are counted
- parameters can be overridden as a third param

`(report.pr-cycle-time/do-it! user repo)`
`(report.pr-cycle-time/do-it! "holochain" "holochain-rust")`
`(report.pr-cycle-time/do-it! user repo params)`
`(report.pr-cycle-time/do-it! "holochain" "holochain-rust" {:base "develop" :state "all"})`
