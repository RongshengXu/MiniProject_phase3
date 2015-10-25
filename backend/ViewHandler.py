from Stream import StreamModel
from google.appengine.ext import db
from Stream import PictureModel, StreamModel, CountViewModel
from google.appengine.api import images
from google.appengine.api import users
from datetime import datetime, date
import webapp2

import os
import jinja2
import json
import re

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

class MoboileViewSingle(webapp2.RequestHandler):
    def get(self):
        stream_name = re.findall("%3D%3D(.+)", self.request.url)[0]
        stream_query = StreamModel.query(StreamModel.name==stream_name).fetch()
        picture_query = db.GqlQuery("SELECT *FROM PictureModel WHERE ANCESTOR IS :1 ORDER BY uploadDate DESC",
                                      db.Key.from_path('StreamModel', stream_name))
        stream = stream_query[0]
        Imagesurl = []
        Imagecap = []
        for picture in picture_query:
            s = "http://sacred-highway-108321.appspot.com/android/getimage==" + stream_name + "===" + str(picture.id)
            Imagesurl.append(s)
            Imagecap.append(str(picture.id))
        # Imagesurl.append("http://static.independent.co.uk/s3fs-public/styles/story_large/public/thumbnails/image/2013/01/24/12/v2-cute-cat-picture.jpg")
        # Imagecap.append("Haha")
        result = {"imageurl":Imagesurl, 'imagecap':Imagecap}
        jsonObj=json.dumps(result, sort_keys=True,indent=4, separators=(',', ': '))
        self.response.headers['Content-Type']="application/json"
        self.response.write(jsonObj)

class MobileGetImage(webapp2.RequestHandler):
    def get(self):
        stream_name = re.findall("%3D%3D(.+)%3D%3D%3D", self.request.url)[0]
        pic_id = re.findall("%3D%3D%3D(.+)", self.request.url)[0]
        pictures = db.GqlQuery("SELECT *FROM PictureModel WHERE ANCESTOR IS :1 ORDER BY uploadDate DESC",
                                      db.Key.from_path('StreamModel', str(stream_name)))
        # print stream_name + "  and " + pic_id
        for picture in pictures:
            if (picture.id==pic_id):
                self.response.out.write(picture.picture)
        self.response.headers['Content-Type'] = 'image/jpeg'
        self.response.out.write(stream_name + " and " + pic_id + "aaa")

app = webapp2.WSGIApplication([
    ('/view', View),
    ('/android/mobileview', MobileView),
    ('/android/mobileviewsingle.*', MoboileViewSingle),
    ('/android/getimage.*', MobileGetImage)
], debug=True)