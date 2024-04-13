#!/usr/bin/env bash

echo '#!/usr/bin/env -S java --source 11' > TrayServer
cat src/java/TrayServer.java >> TrayServer
chmod +x ./TrayServer
