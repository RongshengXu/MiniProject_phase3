from Stream import StreamModel
import webapp2

import os
import jinja2
import json

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

class View(webapp2.RequestHandler):
    more = True
    def get(self):
        template = JINJA_ENVIRONMENT.get_template('templates/view.html')
        stream_query = StreamModel.query().order(StreamModel.createTime).fetch()
        index_info = []
        num = 0
        if len(stream_query) > 0:
            self.more = True
            for stream in stream_query:
                index_info.append((stream.name, stream.coverpageURL, stream.url, num))
                if num==3:
                    num = 0
                else:
                    num += 1
        template_values = {
            'stream_query_len': len(stream_query),
            'index_info': index_info
        }
        self.response.write(template.render(template_values))

class MobileView(webapp2.RequestHandler):
    def get(self):
        # receivedata=json.loads(self.request.body)

        # print receivedata
        # print self.request.body
        # username=receivedata["username"]

        streams=StreamModel.query().order(StreamModel.createTime).fetch()
        names=[]
        coverurls=[]
        for stream in streams:
            names.append(stream.name)
            coverurls.append(stream.coverpageURL)

        result={"streamcoverurls":coverurls, 'streamnames':names}
        jsonObj=json.dumps(result, sort_keys=True,indent=4, separators=(',', ': '))
        self.response.headers['Content-Type']="application/json"
        self.response.write(jsonObj)

app = webapp2.WSGIApplication([
    ('/view', View),
    ('/android/mobileview', MobileView)
], debug=True)