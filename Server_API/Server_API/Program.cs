using Server_API.Models;
using Server_API.Services;
using Server_API.Hubs;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.Configure<CouponVerseDatabaseSettings>(
    builder.Configuration.GetSection("CouponVerseDatabase"));
builder.Services.AddSingleton<UsersService>();
builder.Services.AddSingleton<CouponsService>();
builder.Services.AddSingleton<GroupService>();
builder.Services.AddSingleton<MLModelsService>();

builder.Services.AddSignalR();
builder.Services.AddHttpClient();
builder.Services.AddControllers();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseRouting();
app.UseHttpsRedirection();

app.UseAuthorization();

app.MapHub<GroupsHub>("/groupshub");

app.MapControllers();

app.Run();