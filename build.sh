#!/usr/bin/env bash

echo '#!/usr/bin/env -S java --source 21' > TrayServer
cat src/java/TrayServer.java >> TrayServer
chmod +x ./TrayServer
