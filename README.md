# ci-runtimes

This command-line tool shows Github actions runtimes in a table format.

## Status

This is my personal practice project in Clojure and this tool is work-in-progress.

## Installation

- Clone this repo
- Install `leiningen`
- Create a file `config.edn` in project root and add contents:
    ```
    {:github-token "ghp_your_token"}
    ```
    where ghp_your_token is your personal Github token generated in https://github.com/settings/tokens There create a token with no scopes selected. This is to allow more requests to the github API.
- See Usage for usage instructions

## Usage

Options: `lein run <owner> <repository> "<CI run name>"`

### Example

```
❯ lein run metosin reitit testsuite
Get workflow "testsuite" of metosin/reitit ...
Got workflow id: 5883743
Get jobs of the workflow...

|      :date | :duration |           :line | :runs |
|------------+-----------+-----------------+-------|
| 2021-02-17 |       184 |     ########### |     5 |
| 2021-02-19 |       212 |   ############# |     2 |
| 2021-02-20 |       113 |         ####### |     2 |
| 2021-02-21 |       217 |   ############# |     1 |
| 2021-02-26 |        56 |            #### |     7 |
| 2021-02-27 |         4 |               # |     1 |
| 2021-03-02 |        97 |          ###### |     5 |
| 2021-03-03 |         4 |               # |     2 |
| 2021-03-05 |       125 |        ######## |     1 |
| 2021-03-11 |       187 |     ########### |    13 |
| 2021-03-12 |        73 |           ##### |     2 |
| 2021-04-09 |         4 |               # |     4 |
| 2021-04-12 |         4 |               # |     2 |
| 2021-04-22 |        91 |          ###### |     3 |
| 2021-04-23 |       137 |        ######## |     6 |
| 2021-04-30 |       101 |          ###### |     5 |
| 2021-05-26 |       135 |        ######## |     2 |
| 2021-06-21 |       189 |     ########### |     2 |
| 2021-06-24 |       232 |  ############## |     1 |
| 2021-07-27 |       241 | ############### |     1 |
| 2021-08-03 |       156 |       ######### |    13 |
| 2021-08-05 |       108 |         ####### |     2 |
| 2021-08-30 |         4 |               # |     1 |
| 2021-09-07 |       113 |         ####### |     2 |

Average duration of the workflow runs is 124.63529 seconds
```

## How this works

The inner workings of this tool.

1. Get repository workflows via https://api.github.com//repos/{owner}/{repo}/actions/workflows (https://docs.github.com/en/rest/reference/actions#list-repository-workflows)
1. From the result find workflow ID using its name
1. Get workflow's runs via https://api.github.com/repos/{owner}/{repo}/actions/workflows/{workflow_id}/runs (https://docs.github.com/en/rest/reference/actions#list-workflow-runs)
1. From the result find many job_urls: https://api.github.com/repos/{owner}/{repo}/actions/runs/{run_id}/jobs (https://docs.github.com/en/rest/reference/actions#list-jobs-for-a-workflow-run)
1. For each job_url
    1. Get job's started_at and completed_at strings
    1. Parse a Date object from those
    1. Calculate duration (as seconds) between started_at and completed_at
    1. Store the date (as YYYY-MM-DD string) and duration into a collection
1. For all stored dates and durations group the collection by date
1. For each date calculate average duration
1. For each date calculate the relative duration for illustration purposes (short duration get only a short line and long duration gets a longer line)
1. Print the results for each date

## Limitations

- Gets only the "first" job in from a run which has many jobs.

## License

Copyright © 2021 Kimmo Ahola

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
