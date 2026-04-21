FROM wiremock/wiremock:3.13.2

COPY mappings /home/wiremock/mappings

ENTRYPOINT ["/docker-entrypoint.sh", "--global-response-templating", "--disable-gzip", "--verbose"]
