"""Remove everything the exporter generated.

    python clean.py

Run this once Java has finished with the graph. Nothing here is precious: another
`python export.py "A" "B"` rebuilds all of it.
"""

from config import remove_generated


def main():
    print("cleaning exporter output")
    remove_generated("removed")


if __name__ == "__main__":
    main()
