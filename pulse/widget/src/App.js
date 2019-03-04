import React, { Component } from "react";
import { AreaChart, Area, Legend, Tooltip, XAxis } from "recharts";
import randomColor from "randomcolor";
import data from "./compiled.json";
import moment from "moment";

const START = "2018-01-01T00:00:00Z";
const GRANULARITY = "week";

class App extends Component {
  render() {
    return (
      <div
        style={{
          width: "1200px",
          margin: "auto"
        }}
      >
        <h1>Holochain Pulse</h1>
        {Object.keys(data).map(org => (
          <div key={org}>
            <h2>{org}</h2>
            {Object.keys(data[org]).map(area => (
              <div key={area}>
                <h3>{area}</h3>
                <StackedAreaChart repos={data[org][area]} />
              </div>
            ))}
          </div>
        ))}
      </div>
    );
  }
}

const StackedAreaChart = ({ repos }) => (
  <AreaChart
    width={1200}
    height={100}
    style={{
      fontSize: "80%"
    }}
    data={repos[Object.keys(repos)[0]].map((_, week) => {
      const dataPoint = {
        name: moment(START)
          .startOf(GRANULARITY)
          .add(week, GRANULARITY)
          .format("YYYY/MM/DD")
      };
      for (const repo of Object.keys(repos)) {
        dataPoint[repo] = repos[repo][week];
      }
      return dataPoint;
    })}
    margin={{
      top: 10,
      right: 10,
      left: 0,
      bottom: 0
    }}
  >
    {false && (
      <Legend
        wrapperStyle={{
          paddingLeft: "30px",
          paddingBottom: "35px"
        }}
        layout="vertical"
        align="right"
        verticalAlign="bottom"
        iconType="circle"
        width={200}
      />
    )}
    <XAxis dataKey="name" />
    <Tooltip
      contentStyle={{
        backgroundColor: "white"
      }}
    />
    {Object.keys(repos).map(repo => {
      const color = randomColor();
      return (
        <Area
          key={repo}
          type="monotone"
          dataKey={repo}
          stackId="1"
          fill={color}
          stroke={color}
          width={1000}
        />
      );
    })}
  </AreaChart>
);

export default App;
