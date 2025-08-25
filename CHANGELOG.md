## v2.1.0 (1.21)
### Performance :
- improved modular item checks by 50%
- improved decode performance by 90%
- improved encode performance by 50%
- improved rendering performance by ~20%
  
Ff you encounter any issues create run spark profiler
(try to keep the profiler as contained on the issue as possible)
and either create a GH issue or ping me on discord with the spark link and a description of the issue.


- fixed luminous learning not affecting mob xp.
- fixed crash related to testing enchantability of invalid modular items
- fixed bug where items sometimes would not render on the bench
- fixed crossbow not shooting
- removed ht treechop compat log spam
- re-added epic fight compat
- fixed create options disappearing on reload
- fixed issues in enchanting logic
- fixed emi/jei/rei item tooltip rendering issue
- fixed crash related to skeletons
- fixed bug with create options where the top options would select the wrong thing.
- added support for module-ids as material property keys
- deprecated tag and material_property and merged their usage into module_tag.  
  for now using the old ids will merge into "module_tag"  
  this property replaces both usages
- added "fake_item_identity" (default false) field to "copy_item" ability, allowing to fake the items identity to a degree.
  this might be unsafe and accidentally convert the item to the fake identity, but in some conditions it might also fix 
  the right click behaviour. Use with care.
- reworked ability system and repaired keybinding abilities to be fully functional again.
- added bludgeon property
- fixed mining speed ui not working on neoforge
- fix bug were fake enchantment levels would sometimes not work if the item was not enchanted
- fix bug were sometimes ui stats would not merge correctly
- now preferring vanilla tools for mining level comparison
- added new config options do brighten materials for enchantment glint (Recommended)