## v1.1.49 (1.20.1)
- converted module format to nbt instead of string.
 a new key will be used for the new format "miapi_module_object".
 using the old "miapi_modules" key for the string object will still work,
 but if a miapi_module_object is present it will be read, if not it will fallback to "miapi_modules"
- added command ability
```json
    "ability_context": {
        "command": {
            "command": [
                "say You used the command ability!",
                "effect give @p minecraft:strength 10 1"
            ],
            "asPlayer": true,
            "atPlayer": true,
            "minHold": 30,
            "cooldown": 60,
            "userAnim": "BOW"
        }
    }
```

