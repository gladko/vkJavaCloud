FROM alpine:latest

# Create user to run etcd securely
RUN adduser -D -H -s /sbin/nologin etcd

# Copy the etcd and etcdctl binaries into the container
COPY etcd etcdctl /usr/local/bin/

# Make sure binaries are executable
RUN chmod +x /usr/local/bin/etcd /usr/local/bin/etcdctl

# Create directory for etcd data and assign ownership
RUN mkdir -p /etcd-data && chown etcd:etcd /etcd-data

USER etcd
WORKDIR /etcd-data

# Expose default etcd ports
EXPOSE 2379 2380

# Set the entrypoint to etcd binary
ENTRYPOINT ["/usr/local/bin/etcd"]

# Default arguments to run etcd server
# For cluster deployment add options  "--listen-peer-urls", "http://0.0.0.0:2380"
CMD ["--data-dir", "/etcd-data", "--name", "default", "--listen-client-urls", "http://0.0.0.0:2379", "--advertise-client-urls", "http://0.0.0.0:2379"]