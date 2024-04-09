# tray-server

## Usage

Start the server:
```
$ ./TrayServer
WebServer running at http://localhost:17999
```

Set a different image:

```
$ curl localhost:17999/api?img=green
$ curl localhost:17999/api?img=orange
$ curl localhost:17999/api?img=red
```

Set a different image and link:

```
$ curl "http://localhost:17999/api?img=red&link=http://example.com/something-failed"
```
