<div style="width:100%;padding:0px;margin:0px" align="center">
    <h1>AWS S3 Watchdog</h1>
    <br>
    <a href="https://github.com/niklas2810/aws-s3-watchdog/actions">
    <img alt="GitHub Actions CI" src="https://img.shields.io/github/workflow/status/niklas2810/aws-s3-watchdog/Build%20Main%20Branch%20&%20Docker%20Image?logo=github&style=for-the-badge"/>
    </a>
     <a href="https://app.codacy.com/gh/niklas2810/aws-s3-watchdog/dashboard">
        <img alt="Codacy Analysis" src="https://img.shields.io/codacy/grade/2d9dd890a9d74522a875841dbc040142?logo=codacy&style=for-the-badge"/></a>
    <br>
    <br>   
</div>

&copy; Niklas Arndt 2020 via MIT License

Docker Hub: https://hub.docker.com/r/niklas2810/aws-s3-watchdog

Dependencies:

- [simple-java-mail](https://github.com/bbottema/simple-java-mail)
- [AWS S3 Java SDK](https://github.com/aws/aws-sdk-java)
- [sentry-logback](https://github.com/getsentry/sentry-java)
- [logback-classic](http://logback.qos.ch)
- [dotenv](https://github.com/cdimascio/dotenv-java) 
(only for Debugging purposes, unused in Production)