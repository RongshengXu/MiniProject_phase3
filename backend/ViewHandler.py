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

mobileshow = 0
nearbyshow = 0

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
        global mobileshow
        stream_name = re.findall("%3D%3D(.+)", self.request.url)[0]
        stream_query = StreamModel.query(StreamModel.name==stream_name).fetch()
        picture_query = db.GqlQuery("SELECT *FROM PictureModel WHERE ANCESTOR IS :1 ORDER BY uploadDate DESC",
                                      db.Key.from_path('StreamModel', stream_name))
        stream = stream_query[0]
        Imagesurl = []
        Imagecap = []
        num = stream.totalPicture
        if (num > 16):
            for picture in picture_query:
                if (int(picture.id)>(mobileshow*16) and int(picture.id)<=((mobileshow+1)*16)):
                    s = "http://sacred-highway-108321.appspot.com/android/getimage==" + stream_name + "===" + str(picture.id)
                    Imagesurl.append(s)
                    Imagecap.append(str(picture.id))
            mobileshow += 1
            if mobileshow*16 > num:
                mobileshow = 0
        else:
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

class MobileUpload(webapp2.RequestHandler):
    def get(self):
        stream_name = self.request.params['streamname']
        self.response.write(stream_name)

    def post(self):
        picture = self.request.get('file')
        stream_name = self.request.params['streamname']
        stream_query = StreamModel.query(StreamModel.name==stream_name)
        stream = stream_query.fetch()[0]
        stream.totalPicture = stream.totalPicture + 1
        user_picture = PictureModel(parent = db.Key.from_path('StreamModel', stream_name))
        user_picture.id = str(stream.totalPicture)
        picture = images.resize(picture, 320, 400)
        user_picture.picture = db.Blob(picture)
        stream.lastUpdated = user_picture.uploadDate
        stream.put()
        user_picture.put()
        # self.response.write(stream_name+" and "+type(picture))

class MobileSearchResult(webapp2.RequestHandler):
    def get(self):
        pattern = re.findall("%3D%3D(.+)",self.request.url)[0]
        found_in_tag = False
        Name = []
        streams = StreamModel.query().fetch()
        for st in streams:
            Name.append(st.name)
        display = 0
        streamcoverURLs = []
        streamnames = []
        # infos = []
        for name in Name:
            fi = re.findall(pattern, name)
            stream = StreamModel.query(StreamModel.name==name).fetch()[0]
            for tag in stream.tag:
                if len(re.findall(pattern,tag))>0:
                    found_in_tag = True
            if len(fi)>0 or found_in_tag==True:
                streamcoverURLs.append(stream.coverpageURL)
                streamnames.append(stream.name)
                display += 1
        result = {"streamcoverurls":streamcoverURLs, 'streamnames':streamnames}
        jsonObj=json.dumps(result, sort_keys=True,indent=4, separators=(',', ': '))
        self.response.headers['Content-Type']="application/json"
        self.response.write(jsonObj)

class MobileViewSubscribed(webapp2.RequestHandler):
    def get(self):
        user = re.findall("%3D%3D(.+)",self.request.url)[0]
        streamcoverURLs = []
        streamnames = []
        name = []
        streams = StreamModel.query().fetch()
        for st in streams:
            name = []
            for n in st.subscribers:
                name.append(n.lower())
            if user.lower() in name:
                streamcoverURLs.append(st.coverpageURL)
                streamnames.append(st.name)
        result = {"streamcoverurls":streamcoverURLs, 'streamnames':streamnames}
        jsonObj=json.dumps(result, sort_keys=True,indent=4, separators=(',', ': '))
        self.response.headers['Content-Type']="application/json"
        self.response.write(jsonObj)

class MobileViewNearby(webapp2.RequestHandler):
    def get(self):
        latitude = float(re.findall("%3D%3D(.+)%3D%3D%3D", self.request.url)[0])
        longitude = float(re.findall("%3D%3D%3D(.+)", self.request.url)[0])
        stream_query = StreamModel.query().fetch()
        Imagesurl = []
        Imagecap = []
        streamnames = []
        container = []
        num = 0
        global nearbyshow
        nearbyshow = 0
        for stream in stream_query:
            picture_query = db.GqlQuery("SELECT *FROM PictureModel WHERE ANCESTOR IS :1 ORDER BY uploadDate DESC",
                                      db.Key.from_path('StreamModel', str(stream.name)))
            for picture in picture_query:
                # la = float(picture.lat)
                # lo = float(picture.lg)
                dis = abs(pow((30.28226821-latitude),2.0)+pow((97.7393+longitude),2.0))
                container.append((dis, picture, stream.name))
        container.sort(key=lambda x:x[0])
        for item in container:
            if num < 8:
                pic = item[1]
                # parent = pic.getParent().get()
                s = "http://sacred-highway-108321.appspot.com/android/getimage==" + item[2] + "===" + str(pic.id)
                Imagesurl.append(s)
                Imagecap.append(str(pic.id))
                streamnames.append(item[2])
                num += 1
        # for pic in picture_query:
        #     if (num < 8):
        #         parent = pic.getParent().get()
        #         s = "http://sacred-highway-108321.appspot.com/android/getimage==" + parent.name + "===" + str(pic.id)
        #         Imagesurl.append(s)
        #         Imagecap.append(str(pic.id))
        #         streamnames.append(parent.name)
        #         num += 1
        result = {"imageurl":Imagesurl, 'imagecap':Imagecap, "streamnames": streamnames}
        jsonObj=json.dumps(result, sort_keys=True,indent=4, separators=(',', ': '))
        # self.response.write(container)
        self.response.headers['Content-Type']="application/json"
        self.response.write(jsonObj)

class MobileShowMore(webapp2.RequestHandler):
    def get(self):
        latitude = float(re.findall("%3D%3D(.+)%3D%3D%3D", self.request.url)[0])
        longitude = float(re.findall("%3D%3D%3D(.+)", self.request.url)[0])
        stream_query = StreamModel.query().fetch()
        Imagesurl = []
        Imagecap = []
        streamnames = []
        container = []
        num = 0
        global nearbyshow
        total = 0
        for stream in stream_query:
            picture_query = db.GqlQuery("SELECT *FROM PictureModel WHERE ANCESTOR IS :1 ORDER BY uploadDate DESC",
                                      db.Key.from_path('StreamModel', str(stream.name)))
            for picture in picture_query:
                dis = abs(pow((30.28226821-latitude),2.0)+pow((97.7393+longitude),2.0))
                container.append((dis, picture, stream.name))
                total += 1
        container.sort(key=lambda x:x[0])
        if nearbyshow*8 <= total and (nearbyshow+1)*8 > total:
            nearbyshow = 0
        else:
            nearbyshow += 1
        for item in container:
            if nearbyshow == 0 or (nearbyshow*8< num and (nearbyshow+1)*8 >= num):
                pic = item[1]
                # parent = pic.getParent().get()
                s = "http://sacred-highway-108321.appspot.com/android/getimage==" + item[2] + "===" + str(pic.id)
                Imagesurl.append(s)
                Imagecap.append(str(pic.id))
                streamnames.append(item[2])
            num += 1
        # for pic in picture_query:
        #     if (num < 8):
        #         parent = pic.getParent().get()
        #         s = "http://sacred-highway-108321.appspot.com/android/getimage==" + parent.name + "===" + str(pic.id)
        #         Imagesurl.append(s)
        #         Imagecap.append(str(pic.id))
        #         streamnames.append(parent.name)
        #         num += 1
        result = {"imageurl":Imagesurl, 'imagecap':Imagecap, "streamnames": streamnames}
        jsonObj=json.dumps(result, sort_keys=True,indent=4, separators=(',', ': '))
        # self.response.write(container)
        self.response.headers['Content-Type']="application/json"
        self.response.write(jsonObj)

class tt(webapp2.RequestHandler):
    def get(self):
        self.redirect('/android/mobileviewsubscribed==xurongsheng2010')

app = webapp2.WSGIApplication([
    ('/view', View),
    ('/android/mobileview', MobileView),
    ('/android/mobileviewsingle.*', MoboileViewSingle),
    ('/android/getimage.*', MobileGetImage),
    ('/android/upload', MobileUpload),
    ('/android/searchresult.*', MobileSearchResult),
    ('/android/mobileviewsubscribed.*', MobileViewSubscribed),
    ('/android/viewnearby.*', MobileViewNearby),
    ('/android/showmore.*', MobileShowMore),
    ('/tt', tt)
], debug=True)