require("dotenv").config();
const ApolloClient = require("apollo-boost").default;
const gql = require("graphql-tag");
const { writeFileSync } = require("fs");
const fetch = require("node-fetch");
const { REPOS, START } = require("./config");

const client = new ApolloClient({
  uri: "https://api.github.com/graphql",
  fetch,
  headers: {
    Authorization: `bearer ${process.env.GITHUB_ACCESS_TOKEN}`
  }
});

let data = {};
try {
  data = require("./data.json");
} catch (e) {}

const start = async () => {
  for (const org of Object.keys(REPOS)) {
    const areas = REPOS[org];
    if (!data[org]) {
      data[org] = {};
    }
    for (const area of Object.keys(areas)) {
      const repos = areas[area];
      if (!data[org][area]) {
        data[org][area] = {};
      }
      for (const repo of repos) {
        if (data[org][repo]) {
          continue;
        }
        const commits = [];
        let cursor;
        let hasNextPage = true;
        while (hasNextPage) {
          console.log(repo);
          const res = await client.query({
            query: gql`
              query(
                $owner: String!
                $name: String!
                $since: GitTimestamp!
                $after: String
              ) {
                repository(owner: $owner, name: $name) {
                  defaultBranchRef {
                    target {
                      ... on Commit {
                        history(first: 100, since: $since, after: $after) {
                          edges {
                            node {
                              committedDate
                            }
                            cursor
                          }
                          pageInfo {
                            hasNextPage
                          }
                        }
                      }
                    }
                  }
                }
              }
            `,
            variables: {
              after: cursor,
              owner: org,
              name: repo,
              since: START
            }
          });
          if (
            !res.data.repository.defaultBranchRef.target.history.edges.length
          ) {
            break;
          }
          cursor =
            res.data.repository.defaultBranchRef.target.history.edges[
              res.data.repository.defaultBranchRef.target.history.edges.length -
                1
            ].cursor;
          hasNextPage =
            res.data.repository.defaultBranchRef.target.history.pageInfo
              .hasNextPage;
          commits.push(
            ...res.data.repository.defaultBranchRef.target.history.edges.map(
              e => new Date(e.node.committedDate)
            )
          );
        }
        data[org][repo] = commits;

        writeFileSync("data.json", JSON.stringify(data, null, 2));
      }
    }
  }
};

start().catch(console.error);
