package main

import (
	"encoding/binary"
	"flag"
	"fmt"
	"io"
	"net"
	"time"
)

func main() {
	port := flag.Int("port", 6667, "UDP port to listen on")
	flag.Parse()

	addr := fmt.Sprintf(":%d", *port)
	conn, err := net.ListenPacket("udp", addr)
	if err != nil {
		fmt.Printf("Error starting listener: %v\n", err)
		return
	}
	defer conn.Close()

	fmt.Printf("Listening for UDP broadcasts on port %d...\n", *port)

	buffer := make([]byte, 1024)

	for {
		n, remoteAddr, err := conn.ReadFrom(buffer)
		if err != nil {
			fmt.Printf("Error reading packet: %v\n", err)
			continue
		}

		if err := parseAndPrint(buffer[:n], remoteAddr); err != nil {
			fmt.Printf("Error parsing packet from %s: %v\n", remoteAddr.String(), err)
			continue
		}
	}
}

func parseAndPrint(data []byte, remoteAddr net.Addr) error {
	reader := &byteReader{data: data, pos: 0}

	email, err := readUTF(reader)
	if err != nil {
		return fmt.Errorf("failed to read email: %w", err)
	}

	lastSeenNanos, err := readInt64(reader)
	if err != nil {
		return fmt.Errorf("failed to read lastSeen: %w", err)
	}

	ip, err := readUTF(reader)
	if err != nil {
		return fmt.Errorf("failed to read ip: %w", err)
	}

	port, err := readInt32(reader)
	if err != nil {
		return fmt.Errorf("failed to read port: %w", err)
	}

	timestamp := time.Unix(lastSeenNanos/1_000_000_000, lastSeenNanos%1_000_000_000)

	fmt.Printf("Email:       %s\n", email)
	fmt.Printf("Last Seen:   %s\n", timestamp.Format("2006-01-02 15:04:05.000000000 MST"))
	fmt.Printf("IP:          %s\n", ip)
	fmt.Printf("Port:        %d\n", port)
	fmt.Printf("Source:      %s\n", remoteAddr.String())

	return nil
}

type byteReader struct {
	data []byte
	pos  int
}

func readUTF(r *byteReader) (string, error) {
	if r.pos+2 > len(r.data) {
		return "", io.ErrUnexpectedEOF
	}

	length := binary.BigEndian.Uint16(r.data[r.pos : r.pos+2])
	r.pos += 2

	if r.pos+int(length) > len(r.data) {
		return "", io.ErrUnexpectedEOF
	}

	str := string(r.data[r.pos : r.pos+int(length)])
	r.pos += int(length)

	return str, nil
}

func readInt64(r *byteReader) (int64, error) {
	if r.pos+8 > len(r.data) {
		return 0, io.ErrUnexpectedEOF
	}

	val := int64(binary.BigEndian.Uint64(r.data[r.pos : r.pos+8]))
	r.pos += 8

	return val, nil
}

func readInt32(r *byteReader) (int32, error) {
	if r.pos+4 > len(r.data) {
		return 0, io.ErrUnexpectedEOF
	}

	val := int32(binary.BigEndian.Uint32(r.data[r.pos : r.pos+4]))
	r.pos += 4

	return val, nil
}
