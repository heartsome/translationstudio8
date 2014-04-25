#!/usr/bin/env python3

import os, os.path

def main():
	for root, dirs, files in os.walk('/Users/felix_lu/Documents/r8space'):
		for name in files:
			if name == 'MANIFEST.MF' or name == 'plugin.xml':
				print(os.path.join(root, name))


if __name__ == '__main__':
	main()