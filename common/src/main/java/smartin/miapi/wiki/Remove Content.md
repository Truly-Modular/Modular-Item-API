@header Remove Content
@path /examples/remove-content
@keywords Datapack ,remove,
In case you want to remove specific content, there is a quick way todo so.
Simply create an empty datapack with a mc.meta
https://minecraft.wiki/w/Pack.mcmeta
```
{
  "pack": {
    "pack_format": 48,
    "description": "remove-miapi-materials"
  },
  "filter": {
    "block": [
    {
        "namespace": "miapi",       
        "path": "miapi/materials*" 
      }
    ]
  }
}
```
and block the desired content.
You can check the repositories for the exact path used.  
**Warning** be sure to look for the right version you are using!
Paths changed drastically between 1.20 and 1.21!
