const data = require("./data.json");
const { START, GRANULARITY, REPOS } = require("./config");
const { writeFileSync } = require("fs");
const moment = require("moment");

const start = () => {
  const now = moment();
  const compiled = {};
  for (const org of Object.keys(REPOS)) {
    compiled[org] = {};
    for (const area of Object.keys(REPOS[org])) {
      compiled[org][area] = {};
      for (const repo of REPOS[org][area]) {
        if (!data[org][repo].length) {
          continue;
        }
        compiled[org][area][repo] = [];
        const commits = data[org][repo].reverse();
        let week = moment(START).startOf(GRANULARITY);
        let j = 0;
        let i = 0;
        while (week < now) {
          compiled[org][area][repo][j] = 0;
          while (
            i < commits.length - 1 &&
            moment(commits[i]) < moment(week).add(1, GRANULARITY)
          ) {
            i++;
            compiled[org][area][repo][j]++;
          }
          j++;
          week.add(1, GRANULARITY);
        }
      }
    }
  }
  writeFileSync("compiled.json", JSON.stringify(compiled, null, 2));
};

start();
