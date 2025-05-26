# Spring Integration TCP Client-Server with Length-Prefixed Messages

This project demonstrates a TCP client-server communication using Spring Integration with length-prefixed message framing.

## Overview

- The **Modern** service acts as a TCP client sending JSON messages.
- The **Legacy** service acts as a TCP server receiving and responding to messages.
- Messages are framed with a **length prefix** to avoid delimiter issues like extra empty messages.
- Jackson's `ObjectMapper` serializes/deserializes messages as JSON.

---

## Key Concepts

### Length-Prefixed Framing

Instead of delimiting messages with newline (`\n`), messages are sent with a fixed-size header indicating the length of the message body.

Example:


Spring Integration's `ByteArrayLengthHeaderSerializer` is used to handle this framing automatically.

---

## Setup

### Docker Compose Network

Services run in a user-defined Docker bridge network allowing hostname resolution by service names:

```yaml
networks:
  my-network:
    driver: bridge
```
---
### Running with Docker Compose
Use the provided docker-compose.yml to run both services in the same network.
```yaml
docker-compose up -d --build
```