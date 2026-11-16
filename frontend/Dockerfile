# Runtime-only image: copies dist/ built on the host by scripts/build.sh (no npm inside Docker).
# For a self-contained build on a machine that can reach the npm registry, use Dockerfile.multistage.
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY dist/ /usr/share/nginx/html/
EXPOSE 80
