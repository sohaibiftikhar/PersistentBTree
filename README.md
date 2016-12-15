
# Persistent BTree Implementation
===============================

* Uses Memorymappedfile buffers.
* Currently can only support file sizes upto 2GB but this can easily be extended with an array of Memorymapped buffers instead of one
* Allows for storing Btrees with key of type integer and value a byte array.
* Allows range queries between a key range.
