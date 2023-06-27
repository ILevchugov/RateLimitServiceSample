# RateLimitServiceSample
Spring Boot Service Sample with Rate Limit implementation by sliding window algorithm 

# How to run
docker build -t service-sample .

docker run -p 8080:8080 service-sample

# How to check
GET http://localhost:8080/ping 

# How to change parameters of rate limit

application.yml ->
rate-limit.time and rate-limit.request-count represents how much requests (rate-limit.request-count) allowed per rate-limit.time(in seconds)
