"""Remove everything the exporter generated.

    python clean.py

Run this once Java has finished with the graph. Nothing here is precious: another
`python export.py "A" "B"` rebuilds all of it.
"""

from config import GENERATED_FILES, remove


def main():
    print("cleaning exporter output")
    remove(GENERATED_FILES, "removed")


if __name__ == "__main__":
    main()
