package com.rocksoft.curly

import spock.lang.Specification

class CurlySpec extends Specification {

  def "Normalizes URLs"() {
    when:
    String url = Curly.normalizeUrl("http://www.rocksoftcode.com#/test")

    then:
    url == "http://www.rocksoftcode.com/test"

    when:
    url = Curly.normalizeUrl("http://www.somespaces.com/spaced out stuff")

    then:
    url == "http://www.somespaces.com/spaced%20out%20stuff"
  }

  def "Gets status 'line'"() {
    when:
    String statusLine = Curly.readStatusLine("<html></html>\n400 text/html")

    then:
    statusLine == "400 text/html"

    when:
    statusLine = Curly.readStatusLine('''<html></html>\n\n200''')

    then:
    statusLine == "200"

    when:
    statusLine = Curly.readStatusLine("200 text/html 801039")

    then:
    statusLine == "200 text/html 801039"

    when:
    statusLine = Curly.readStatusLine("200 text/html;charset=utf-8 801039")

    then:
    statusLine == "200 text/html;charset=utf-8 801039"

    when:
    statusLine = Curly.readStatusLine("301")

    then:
    statusLine == "301"

    when:
    statusLine = Curly.readStatusLine("</someXml>\n 200")

    then:
    statusLine == "200"

    when:
    statusLine = Curly.readStatusLine(null)

    then:
    statusLine == null
  }

  def "Cleans up charset on content-type"() {
    when:
    String statusLine = Curly.readStatusLine("200 text/html; charset=utf-8 801039")

    then:
    statusLine == "200 text/html;charset=utf-8 801039"

    when:
    statusLine = Curly.readStatusLine("200 text/html;   charset=utf-8 801039")

    then:
    statusLine == "200 text/html;charset=utf-8 801039"

    when:
    statusLine = Curly.readStatusLine("200 text/foo;   charset=utf-8 801039")

    then:
    statusLine == "200 text/foo;charset=utf-8 801039"
  }

  def "Parses a HEAD response into an object"() {
    setup:
    String mockResponseText = new File("src/test/resources/simple-head-response.txt").text

    when:
    CurlHeadResponse response = Curly.parseHeadResponse(mockResponseText)

    then:
    response.httpStatusCode == 200
    response.getHeader(HttpHeader.CONTENT_TYPE) == ['text/html; charset=utf-8']
    response.getHeader(HttpHeader.CONTENT_LENGTH) == ['849']
    response.getHeader(HttpHeader.SET_COOKIE) == ['BCSI-CS-8caa5bd26cfb5782=2; Path=/']
    response.getHeader('Cache-Control') == ['no-cache']
    response.getHeader('Pragma') == ['no-cache']
    response.getHeader('Connection') == ['close']
  }

  def "Parses all entries in a chained HEAD response into an object, returning last status"() {
    setup:
    String mockResponseText = new File("src/test/resources/chained-head-response.txt").text

    when:
    CurlHeadResponse response = Curly.parseHeadResponse(mockResponseText)

    then:
    response.httpStatusCode == 200
    response.getHeader(HttpHeader.CONTENT_TYPE) == ["text/html; charset=UTF-8", "text/html; charset=UTF-8", "text/html; charset=utf-8"]
    response.getHeader(HttpHeader.CONTENT_LENGTH) == ['849']
    response.getHeader(HttpHeader.SET_COOKIE) == ['BCSI-CS-8caa5bd26cfb5782=2; Path=/']
    response.getHeader('Cache-Control') == ["private, max-age=0", "private, max-age=0", "no-cache"]
    response.getHeader('Pragma') == ['no-cache']
    response.getHeader('Connection') == ['Keep-Alive', 'Keep-Alive', 'close']
  }

  def "Returns location field and date correctly"() {
    setup:
    String mockResponseText = new File("src/test/resources/simple-head-response-2.txt").text

    when:
    CurlHeadResponse response = Curly.parseHeadResponse(mockResponseText)

    then:
    response.getHeader(HttpHeader.LOCATION).size() == 1
    response.getHeader(HttpHeader.LOCATION).first() == "http://corporate.target.com?ref=sr_shorturl_about"
    response.getHeader(HttpHeader.DATE).size() == 1
    response.getHeader(HttpHeader.DATE).first() == "Thu, 09 Oct 2014 20:44:50 GMT"
  }

  def "Why??"() {
    expect:
    Curly.forHead("http://target.com/about").getHeader(HttpHeader.LOCATION)
  }
}