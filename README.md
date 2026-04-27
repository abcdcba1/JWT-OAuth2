JWTの理解とOauth2とインフラ（ＡＷＳ），ｃｉｃｄ機能を練習為のtoy project

Backend: Java 17, Spring Boot 3.x, Spring Security, JPA

Frontend: CSS

Database: MySQL, H2 (Test)

Infrastructure: AWS Elastic Beanstalk(EC2, RDS), GitHub Actions (CI/CD)

認証 flow: OAuth2を通じてGoogleログインを実装して、サーバーからJWTを発行してStatelessに処理

配布自動化: GitHub Actionsを使ってpushする場合AWS Elastic Beanstalkを通じて自動配布するCI/CDパイプライン構築

一番勉強になったところ
私はSpring Securityというものを使う時, Session方式でform loginをしていて自動でログインが処理されて
実行フローをよくわからなかったが、
JWTを使ってUsernamePasswordAuthenticationFilterから始まる認証の流れをカスタムで実装して理解するようになったこと
UsernamePasswordAuthenticationFilter：ログイン試し
AuthenticationManager：アカウント検証
成功ハンドラーか失敗ハンドラーが実行されること
